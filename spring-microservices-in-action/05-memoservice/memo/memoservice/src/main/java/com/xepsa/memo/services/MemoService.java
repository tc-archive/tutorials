package com.xepsa.memo.services;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import com.xepsa.memo.config.ServiceConfig;
import com.xepsa.memo.model.Memo;
import com.xepsa.memo.repository.MemoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.UUID;

@Service
@RefreshScope
public class MemoService {

    @Autowired
    private MemoRepository memoRepository;

    @Autowired
    ServiceConfig config;

    // curl http://localhost:8081/v1/user/user-id/memo/memo-id
    @HystrixCommand(
            fallbackMethod = "handleGetMemoFailure",
            threadPoolKey = "licenseByOrgThreadPool",
            threadPoolProperties = {
                    @HystrixProperty(name = "coreSize", value="30"),
                    @HystrixProperty(name ="maxQueueSize", value="10")
            },
            commandProperties = {
                    @HystrixProperty(
                            name="execution.isolation.thread.timeoutInMilliseconds",
                            value="6000")
            }
    )
    public Memo getMemo(String memoId) {
        simulateIntermittentLongRunningInvocation();
        // Memo memo = memoRepository.findById(memoId);
        Memo memo = mockMemo();
        return memo.withExtraInfo(config.getExampleProperty());
    }

    public void saveMemo(Memo memo) {
        memo.withId(UUID.randomUUID().toString());
        memoRepository.save(memo);
    }

    public void updateMemo(Memo memo) {
        memoRepository.save(memo);
    }

    public void deleteMemo(Memo memo) {
        memoRepository.delete(memo.getId());
    }


    private Memo handleGetMemoFailure(String memoId) {
        final String title = "Failed to retrieved Memo";
        final String message = "Failed to retrieved Memo. This should probably be an exception.";
        return new Memo().
                withTitle(title).
                withMessage(message);
    }

    private Memo mockMemo() {
        final String id = UUID.randomUUID().toString();
        final String userId = UUID.randomUUID().toString();
        final String title = "the title";
        final String message = "some message";
        return new Memo().
                withId(id).
                withUserId(userId).
                withTitle(title).
                withMessage(message);
    }

    private void simulateIntermittentLongRunningInvocation() {
        Random rand = new Random();
        int randomNum = rand.nextInt(3) + 1;
        if (randomNum==3) {
            sleep();
        }
    }

    private void sleep(){
        try {
            Thread.sleep(11000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
