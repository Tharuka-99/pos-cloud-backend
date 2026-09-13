package com.blackholesoftware.pos.repository;

import com.blackholesoftware.pos.entity.BaseSyncEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;
import java.io.Serializable;
import java.util.List;

@NoRepositoryBean
public interface BaseSyncRepository<T extends BaseSyncEntity, ID extends Serializable> extends JpaRepository<T, ID> {
    List<T> findByIsSyncedFalse();
}