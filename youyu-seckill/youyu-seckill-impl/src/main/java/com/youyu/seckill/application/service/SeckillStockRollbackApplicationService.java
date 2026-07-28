package com.youyu.seckill.application.service;

import com.youyu.seckill.api.dto.SeckillStockRollbackMessage;
import com.youyu.seckill.domain.service.SeckillStockDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SeckillStockRollbackApplicationService {

    private final SeckillStockDomainService stockDomainService;

    public void rollback(SeckillStockRollbackMessage message) {
        stockDomainService.rollbackStock(message.getProductId(), message.getQuantity());
        stockDomainService.rollbackUserPurchase(
            message.getUserId(),
            message.getProductId(),
            message.getQuantity()
        );
    }
}