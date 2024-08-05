package com.allst.boot.toobj;

import com.allst.boot.entity.Person;
import com.allst.boot.model.PersonBo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

/**
 * @author Hutu
 * @since 2024-08-05 下午 09:48
 */
@Mapper
public interface PersonMapper {
    PersonMapper INSTANCE = Mappers.getMapper(PersonMapper.class);
    @Mappings({
            @Mapping(source = "id", target = "id"),
            @Mapping(source = "name", target = "name"),
            @Mapping(source = "age", target = "age"),
            @Mapping(source = "address", target = "address")
    })
    PersonBo personToPersonDto(Person person);
}