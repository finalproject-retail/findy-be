package com.retail.map_service.entity;

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
@Table(name = "beacons")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class BeaconEntity extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "beacon_id")
	private Long beaconId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "store_id", nullable = false)
	private StoreEntity store;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "grid_id", nullable = false)
	private GridEntity grid;

	@Column(name = "beacon_uuid", nullable = false, length = 255)
	private String beaconUuid;

	/** iBeacon major; 실물=40011, 가상=store_id */
	@Column(name = "major")
	private Integer major;

	/** iBeacon minor; 실물=BeaconSET 값, 가상=grid_id */
	@Column(name = "minor")
	private Integer minor;

	/** BLE MAC; 실물=Minew, 가상=FA:00:01:00:.. */
	@Column(name = "mac", length = 17)
	private String mac;
}
