package com.emoldino.framework.terminology.dictionary;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

public class IField {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	Long id;

	Long idealShotCount;

}
