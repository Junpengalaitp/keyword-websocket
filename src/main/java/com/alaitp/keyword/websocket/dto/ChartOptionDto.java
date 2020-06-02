package com.alaitp.keyword.websocket.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * the top keywords and their counts are updated after every job description analyzed for each category.
 */
@Data
@AllArgsConstructor
public class ChartOptionDto {
    private Object[] keyword;
    private Object[] count;
    private String category;
}
