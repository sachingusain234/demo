package com.example.demo.config;

import com.example.demo.Model.Data;
import org.springframework.batch.item.ItemProcessor;

public class DataProcessor implements ItemProcessor<Data,Data> {
    @Override
    public Data process(Data data) throws Exception{
        return data;
    }
}
