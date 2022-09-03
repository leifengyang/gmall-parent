package com.atguigu.gmall.search;


import com.atguigu.gmall.search.bean.Person;
import com.atguigu.gmall.search.repository.PersonRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;

import java.util.List;
import java.util.Optional;

@SpringBootTest
public class EsTest {

    @Autowired
    PersonRepository personRepository;

    @Autowired
    ElasticsearchRestTemplate esRestTemplate;

    @Test
    void queryqTest(){
        //索引： 数据库
        //索引一条数据，指给es中保存一条数据
    }



    @Test
    void aaaTest(){
        List<Person> aaaaaa = personRepository.findhaHA("北京市");
        for (Person person : aaaaaa) {
            System.out.println(person);
        }
    }

    /**
     * GET /person/_search
     * {
     *   "query": {
     *     "match": {
     *       "address": "北京市"
     *     }
     *   }
     * }
     */
    @Test
    void queryTest(){
//        Optional<Person> byId = personRepository.findById(2L);
//        System.out.println(byId.get());

        //1、查询 address 在北京市的人
        List<Person> 北京市 = personRepository.findAllByAddressLike("北京市");
//        for (Person person : 北京市) {
//            System.out.println(person);
//        }

        //2、查询 年龄小于等于 19的人
        List<Person> all = personRepository.findAllByAgeLessThanEqual(19);
//        for (Person person : all) {
//            System.out.println(person);
//        }

        //3、查询 年龄 大于 18 且 在北京市的人
        List<Person> 上海市 = personRepository.findAllByAgeGreaterThanAndAddressLike(18, "上海市");
//        for (Person person : 上海市) {
//            System.out.println(person);
//        }
        //4、查询 年龄 大于 18 且 在北京市的人  或  id=3的人
        List<Person> 北京市1 = personRepository.findAllByAgeGreaterThanAndAddressLikeOrIdEquals(18, "北京市", 3L);
        for (Person person : 北京市1) {
            System.out.println(person);
        }

    }


    @Test
    void saveTest(){

        Person person1 = new Person();
        person1.setId(1L);
        person1.setFirstName("三555");
        person1.setLastName("张");
        person1.setAge(19);
        person1.setAddress("北京市昌平区");

        Person person = new Person();
        person.setId(2L);
        person.setFirstName("三");
        person.setLastName("张");
        person.setAge(19);
        person.setAddress("北京市朝阳区");

        Person person2 = new Person();
        person2.setId(3L);
        person2.setFirstName("四");
        person2.setLastName("力");
        person2.setAge(19);
        person2.setAddress("上海市松江区");


        Person person3 = new Person();
        person3.setId(4L);
        person3.setFirstName("三2");
        person3.setLastName("张");
        person3.setAge(20);
        person3.setAddress("北京市天安门");
        personRepository.save(person1);
        personRepository.save(person);
        personRepository.save(person2);
        personRepository.save(person3);

        System.out.println("完成...");

    }
}
