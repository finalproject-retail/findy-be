package com.retail.map_service.entity;

import java.time.OffsetDateTime;

import com.retail.map_service.global.entity.BaseTimeEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "beacon_signal_logs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class BeaconSignalLogEntity extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "beacon_signal_log_id")
	private Long beaconSignalLogId;

	@Column(name = "user_id", nullable = false)
	private Long userId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "store_id", nullable = false)
	private StoreEntity store;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "beacon_id", nullable = false)
	private BeaconEntity beacon;

	@Column(name = "timestamp_iso")
	private OffsetDateTime timestampIso;

	@Column(length = 17)
	private String mac;

	@Column(name = "bluetooth_address_hex", length = 14)
	private String bluetoothAddressHex;

	private Integer rssi;

	@Column(length = 36)
	private String uuid;

	private Integer major;

	private Integer minor;

	private Integer tx;

	/** 신호 기준 가장 가까운 격자 (grids.grid_id) */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "nearest_grid_id")
	private GridEntity nearestGrid;
}
