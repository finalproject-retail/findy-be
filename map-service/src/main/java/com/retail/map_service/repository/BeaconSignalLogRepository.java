package com.retail.map_service.repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.retail.map_service.entity.BeaconSignalLogEntity;
import com.retail.map_service.repository.projection.GridCongestionProjection;

public interface BeaconSignalLogRepository extends JpaRepository<BeaconSignalLogEntity, Long> {

	Optional<BeaconSignalLogEntity> findTopByUserIdAndStore_StoreIdOrderByTimestampIsoDesc(
		Long userId,
		Long storeId
	);

	@Query("""
		select count(distinct log.userId)
		from BeaconSignalLogEntity log
		where log.store.storeId = :storeId
			and log.nearestGrid is not null
			and log.timestampIso >= :from
			and log.timestampIso = (
				select max(latest.timestampIso)
				from BeaconSignalLogEntity latest
				where latest.userId = log.userId
					and latest.store.storeId = :storeId
					and latest.nearestGrid is not null
			)
		""")
	long countActiveUsersByStore(
		@Param("storeId") Long storeId,
		@Param("from") OffsetDateTime from
	);

	@Query("""
		select
			log.nearestGrid.gridId as gridId,
			log.nearestGrid.gridX as gridX,
			log.nearestGrid.gridY as gridY,
			count(distinct log.userId) as activeUserCount
		from BeaconSignalLogEntity log
		where log.store.storeId = :storeId
			and log.nearestGrid is not null
			and log.timestampIso >= :from
			and log.timestampIso = (
				select max(latest.timestampIso)
				from BeaconSignalLogEntity latest
				where latest.userId = log.userId
					and latest.store.storeId = :storeId
					and latest.nearestGrid is not null
			)
		group by log.nearestGrid.gridId, log.nearestGrid.gridX, log.nearestGrid.gridY
		order by activeUserCount desc, log.nearestGrid.gridId asc
		""")
	List<GridCongestionProjection> findGridCongestionByStore(
		@Param("storeId") Long storeId,
		@Param("from") OffsetDateTime from
	);
}
