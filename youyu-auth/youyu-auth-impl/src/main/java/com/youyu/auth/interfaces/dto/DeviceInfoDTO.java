package com.youyu.auth.interfaces.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 设备信息DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceInfoDTO {
    
    private Long id;
    private Long deviceId; // UserDevice表的id，Long类型
    private String deviceName;
    private String os;
    private String browser;
    private String ip;
    private LocalDateTime loginTime;
    private Integer status; // 1-在线 0-已登出
}
