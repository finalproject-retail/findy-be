package com.retail.map_service.dto.request;

import jakarta.validation.constraints.NotEmpty;

public record GridRequest(
        @NotEmpty int[][] grid
) {
}
