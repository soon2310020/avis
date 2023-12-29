package com.emoldino.framework.repository.cachedata;

import java.time.*;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

import org.hibernate.annotations.*;
import org.springframework.data.annotation.*;

import com.fasterxml.jackson.annotation.*;

import lombok.*;

@Entity
@Table(indexes = @Index(name = "UX_CACHE_DATA", columnList = "cacheName,cacheKey", unique = true))
@DynamicUpdate
@Data
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
@NoArgsConstructor
public class CacheData {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String cacheName;
	private String cacheKey;
	@Lob
	private String content;

	private Instant lastUsedAt;
	private Instant expiredAt;
	@CreatedDate
	private Instant createdAt;
}
