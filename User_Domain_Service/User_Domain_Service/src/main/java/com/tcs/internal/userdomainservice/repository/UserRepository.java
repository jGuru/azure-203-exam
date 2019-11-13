package com.tcs.internal.userdomainservice.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.tcs.internal.userdomainservice.model.User;

@Repository
public interface UserRepository extends CrudRepository<User, Integer>
{

}
