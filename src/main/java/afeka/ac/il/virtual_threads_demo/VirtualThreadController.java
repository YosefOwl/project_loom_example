package afeka.ac.il.virtual_threads_demo;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


// https://github.com/olegonsoftware/boot-vt-benchmark/tree/main/src/main/java/guru/oleg/boot_vt

@RestController
@RequestMapping("/")
public class VirtualThreadController {
    public static final int SLEEP_TIME = 5000;

    @GetMapping("/")
    public Mono<String> getResponse() {
        // Start a virtual thread to execute a task
        Thread vt = Thread.startVirtualThread(() -> {
            // This block runs in a virtual thread
            System.err.println(LocalDateTime.now() + " : thread id " + Thread.currentThread().threadId() + " start and sleep");
            try {
                // Simulate some work being done by sleeping
                TimeUnit.MILLISECONDS.sleep(SLEEP_TIME);
            } catch (InterruptedException e) {
                // If the thread is interrupted, throw a RuntimeException
                throw new RuntimeException(e);
            }
            // After sleeping, print a message indicating sleep finished
            System.err.println(LocalDateTime.now() + " : thread id " + Thread.currentThread().threadId() + " sleep finished");
        });

        // Print the thread ID of the main thread and the virtual thread
        System.err.println(LocalDateTime.now() + " : " + "main thread id : " + Thread.currentThread().threadId() + " your thread id : " + vt.threadId());

        // Return a Mono with a greeting message
        return Mono.just("Hello from Spring Boot with Project Loom!");
    }


    // Creating a virtual thread per task executor
    private final ExecutorService virtualExecutor = Executors.newVirtualThreadPerTaskExecutor();

    // Handling GET requests to "/vt" endpoint
    @GetMapping("/vt")
    public Mono<String> handleVirtualThread() {
        // Creating a Mono that emits a single item asynchronously
        return Mono.create(sink -> {
            // Submitting a task to the virtual executor
            virtualExecutor.submit(() -> {
                // Task executed on a virtual thread
                long tId = Thread.currentThread().threadId();
                // Emitting the result of the task through the Mono sink
                sink.success("Executing task on a virtual thread - Thread ID: " + tId);
            });
        });
    }


    @GetMapping("/flux")
    public Flux<String> handleVirtualThreads() {

        return Flux.create(sink -> {
            for (int i = 1; i <= 5; i++) {
                int taskNumber = i;
                virtualExecutor.submit(() -> {
                    // Task executed on a virtual thread
                    long threadId = Thread.currentThread().threadId();
                    sink.next("Executing task " + taskNumber + " on a virtual thread - Thread ID: " + threadId + "\n");
                    if (taskNumber == 5) {
                        sink.complete();
                    }
                });
            }
        });
    }

    //    @GetMapping("/virtual-threads")
    //    public Flux<String> handleVirtualThreads() {
    //        return Flux.range(1, 5)
    //                .flatMap(taskNumber -> {
    //                    return Flux.defer(() -> {
    //                        return Flux.just("Executing task " + taskNumber + " on a virtual thread - Thread ID: " + Thread.currentThread().threadId())
    //                                .subscribeOn(Schedulers.fromExecutor(virtualExecutor));
    //                    });
    //                });
    //    }


}