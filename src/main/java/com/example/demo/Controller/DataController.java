package com.example.demo.Controller;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DataController {
    @Autowired
    private JobLauncher jobLauncher;
    @Autowired
    Job job;
    @GetMapping("/data")
    public ResponseEntity<?> loadDataToDb() throws Exception{
        JobParameters jobParams = new JobParametersBuilder()
                .addLong("startAt", System.currentTimeMillis()).toJobParameters();
        jobLauncher.run(job, jobParams);
        return new ResponseEntity<>("Batch Job has been triggered sucessfully",HttpStatus.OK);
    }
}
