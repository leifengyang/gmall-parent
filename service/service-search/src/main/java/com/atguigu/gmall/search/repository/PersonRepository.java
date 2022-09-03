package com.atguigu.gmall.search.repository;


import com.atguigu.gmall.search.bean.Person;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.concurrent.Future;

@Repository //@Controller、@Service、@Component、@Repository  T, ID
public interface PersonRepository extends ElasticsearchRepository<Person,Long> {

    //SpringData：起名工程师
    //查询 address 在北京市的人
    List<Person> findAllByAddressLike(String address);

    //查询 年龄小于等于 19的人
    List<Person> findAllByAgeLessThanEqual(Integer age);

    //查询 年龄 大于 18 且 在北京市的人
    List<Person> findAllByAgeGreaterThanAndAddressLike(Integer age, String address);

    //查询 年龄 大于 18 且 在北京市的人  或  id=3的人
    //有OR以后会有歧义。处理歧义
//    @Async
//    Future<>
    List<Person> findAllByAgeGreaterThanAndAddressLikeOrIdEquals(Integer age, String address, Long id);


    //DSL
    @Query("{\n" +
            "    \"match\": {\n" +
            "      \"address\": \"?0\"\n" +
            "    }\n" +
            "  }")
    List<Person> findhaHA(String add);

//    long countByAgeGreaterThan(Integer age);

}
