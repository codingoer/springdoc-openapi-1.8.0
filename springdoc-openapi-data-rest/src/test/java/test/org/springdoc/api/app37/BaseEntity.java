package test.org.springdoc.api.app37;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

/**
 * @author bnasslahsen
 */

@Data
@MappedSuperclass
@DynamicInsert(true)
@DynamicUpdate(true)
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
public abstract class BaseEntity implements Serializable {

	/** SVUDI */
	private static final long serialVersionUID = 1L;

	/** 数据库中的ID */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID", unique = true, nullable = false, insertable = false, updatable = false)
	private Long id;


	/**
	 * 数据是否合法
	 *
	 * @return 是否合法
	 */
	public abstract boolean isValid();
}
