package com.stg.service;

import javax.validation.Valid;

@Valid
public interface CreateOrUpdateService<I, O> {

    O create(I input);

    O update(I input);

}
