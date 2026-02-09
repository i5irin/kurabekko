package com.i5irin.kurabekko.infra.cosmos.repository;

import com.azure.spring.data.cosmos.repository.CosmosRepository;
import com.i5irin.kurabekko.infra.cosmos.model.EntryEntity;
import java.util.List;

public interface EntryRepository extends CosmosRepository<EntryEntity, String> {
  List<EntryEntity> findTop20ByUserIdOrderByCreatedAtDesc(String userId);
}
