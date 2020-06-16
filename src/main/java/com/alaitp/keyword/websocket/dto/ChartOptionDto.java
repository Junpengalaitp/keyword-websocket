package com.alaitp.keyword.websocket.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * the top keywords and their counts are updated after every job description analyzed for each category.
 */
@Data
@AllArgsConstructor
public class ChartOptionDto {
    private List<String> keyword;
    private List<Integer> count;
    private String category;
}
