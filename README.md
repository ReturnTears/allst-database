# 机会总是留给有准备的人， 但那往往是努力的人剩下来的 -- 当幸福来敲门
# db
```
DataBase
```
## es
[elastic search](allst-es/README-ES.md)

## luence
[Luence](allst-luence/README.md)

## mgo
[mangodb](allst-mongodb/README.md)

## mysql
Part1: [MySQL](allst-mysql/README-MYSQL.md)

Part2: [MySQL](allst-mysql/README-MYSQL2.md)

## neo4j

[Ne04j](allst-neo4j/README.md)

## odps
[ODPS](allst-odps/README.md)

## Oracle
[Oracle 11g](allst-oracle/README.md)

## pg
[postgreSQL](allst-postgresql/README.md)

## redis
[Redis](allst-redis/README-REDIS2.md)

[Redis](allst-redis/README-REDIS.md)

## sharding
[Sharding](allst-sharding-proxy/README.md)


## git 提交错误解决
```text
fatal: unable to access 'https://github.com/xxx/xxxx.git/': OpenSSL SSL_read: Connection was reset, errno 10054
git config --global http.sslVerify "false"
或者
git config --global --unset http.proxy
```

## Mybatis
```text
  begin
    insert into oracle_table ( id, code ) values( 1 , '1' );  
    insert into oracle_table ( id, code ) values( 2 , '2' );  
    insert into oracle_table ( id, code ) values( 3 , '3' );   
    insert into oracle_table ( id, code ) values( 4 , '4' );
  end;

 <update id="upRroleTable" useGeneratedKeys="false" parameterType="java.util.HashMap">
  begin
    <foreach collection="list" item="info" index="list" separator=";">
        update ${tableName}
        <set>
            VAL_CD=#{info.VAL_CD},
            VAL_NM=#{info.VAL_NM},
            VAL=#{info.VAL},
            VAL_ID=#{info.VAL_ID},
            ruleset=#{info.ruleset}
        </set>
        where VAL_ID =#{info.VAL_ID}
    </foreach>
    ;end;
</update>
```