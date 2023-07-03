package edu.npu.service;

public interface GeneratePaymentService {
    void generateDepartmentPayment(Long shardIndex, int shardTotal);

    void generateUserPayment(Long shardIndex, int shardTotal);
}
