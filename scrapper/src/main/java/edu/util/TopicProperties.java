package edu.util;

public record TopicProperties(
    int partitions,
    int replicas
){}
