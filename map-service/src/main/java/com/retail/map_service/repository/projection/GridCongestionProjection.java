package com.retail.map_service.repository.projection;

public interface GridCongestionProjection {

	Long getGridId();

	Integer getGridX();

	Integer getGridY();

	Long getActiveUserCount();
}
