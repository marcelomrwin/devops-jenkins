package com.redhat.devops.entity;

import javax.persistence.Embeddable;
import javax.persistence.Embedded;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class Address {
	private String street;
	private String suite;
	private String city;
	private String zipcode;
	@Embedded
	private Geo geo;
}
