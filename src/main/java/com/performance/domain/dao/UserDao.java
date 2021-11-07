package com.performance.domain.dao;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.performance.domain.entity.UserMaster;

@Repository
public class UserDao {

    private JdbcTemplate jdbcTemplate;
    
    public UserDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public int searchCount() {
        String sql = "SELECT COUNT(*) FROM user_info";
        
        return jdbcTemplate.queryForObject(sql, Integer.class);
    }

    public void truncateUserInfo() {
        String sql = "TRUNCATE TABLE user_info";
        jdbcTemplate.execute(sql);
    }
    
    public void truncateUserHobby() {
        String sql = "TRUNCATE TABLE user_hobby";
        jdbcTemplate.execute(sql);
    }

    @Transactional
	public void insertUserInfoAndUserHobby(List<UserMaster> insertUserMasterList) {
        StringBuilder sb = new StringBuilder();
        sb.append("WITH info AS ( ");
        sb.append("  INSERT ");
        sb.append("  INTO user_info( ");
        sb.append("    last_name");
        sb.append("    , first_name");
        sb.append("    , prefectures");
        sb.append("    , city");
        sb.append("    , blood_type");
        sb.append("  ) ");
        sb.append("  VALUES (? , ? , ? , ? , ? ) RETURNING id");
        sb.append(") ");
        sb.append("INSERT ");
        sb.append("INTO user_hobby(id, hobby1, hobby2, hobby3, hobby4, hobby5) ");
        sb.append("VALUES ((SELECT id FROM info), ? , ? , ? , ? , ? )");
        jdbcTemplate.batchUpdate(sb.toString(), new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                UserMaster userMaster = insertUserMasterList.get(i);
                ps.setString(1, userMaster.getLastName());
                ps.setString(2, userMaster.getFirstName());
                ps.setString(3, userMaster.getPrefectures());
                ps.setString(4, userMaster.getCity());
                ps.setString(5, userMaster.getBloodType());
                ps.setString(6, userMaster.getHobby1());
                ps.setString(7, userMaster.getHobby2());
                ps.setString(8, userMaster.getHobby3());
                ps.setString(9, userMaster.getHobby4());
                ps.setString(10, userMaster.getHobby5());
            }
            @Override
            public int getBatchSize() {
              return insertUserMasterList.size();
            }
          });
	}

    public UserMaster getTargetUserMaster() {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT");
        sb.append("  i.id");
        sb.append("  , i.last_name");
        sb.append("  , i.first_name");
        sb.append("  , i.prefectures");
        sb.append("  , i.city");
        sb.append("  , i.blood_type");
        sb.append("  , h.hobby1");
        sb.append("  , h.hobby2");
        sb.append("  , h.hobby3");
        sb.append("  , h.hobby4");
        sb.append("  , h.hobby5 ");
        sb.append("FROM");
        sb.append("  user_info i ");
        sb.append("  INNER JOIN user_hobby h ");
        sb.append("    ON i.id = h.id ");
        sb.append("WHERE");
        sb.append("  i.last_name = '試験' ");
        sb.append("  AND i.first_name = '太郎'");
        RowMapper<UserMaster> mapper = new BeanPropertyRowMapper<UserMaster>(UserMaster.class);
        return jdbcTemplate.queryForObject(sb.toString(), mapper);
    }

    public void dropIndex() {
        String sql = "DROP INDEX IF EXISTS idx_user_info_1";
        jdbcTemplate.execute(sql);
    }

    public void createIndex() {
        String sql = "CREATE INDEX IF NOT EXISTS idx_user_info_1 ON user_info(last_name, first_name)";
        jdbcTemplate.execute(sql);
   }

    public List<UserMaster> searchUserMaster(UserMaster targetUserMaster) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT");
        sb.append("  i.id");
        sb.append("  , i.last_name");
        sb.append("  , i.first_name");
        sb.append("  , i.prefectures");
        sb.append("  , i.city");
        sb.append("  , i.blood_type");
        sb.append("  , h.hobby1");
        sb.append("  , h.hobby2");
        sb.append("  , h.hobby3");
        sb.append("  , h.hobby4");
        sb.append("  , h.hobby5 ");
        sb.append("FROM");
        sb.append("  user_info i ");
        sb.append("  INNER JOIN user_hobby h ");
        sb.append("    ON i.id = h.id ");
        sb.append("WHERE");
        sb.append("  i.id <> ? ");
        sb.append("  AND i.blood_type = ? ");
        sb.append("  AND ( ");
        sb.append("    ? IN ( ");
        sb.append("      h.hobby1");
        sb.append("      , h.hobby2");
        sb.append("      , h.hobby3");
        sb.append("      , h.hobby4");
        sb.append("      , h.hobby5");
        sb.append("    ) ");
        sb.append("    OR ? IN ( ");
        sb.append("      h.hobby1");
        sb.append("      , h.hobby2");
        sb.append("      , h.hobby3");
        sb.append("      , h.hobby4");
        sb.append("      , h.hobby5");
        sb.append("    ) ");
        sb.append("    OR ? IN ( ");
        sb.append("      h.hobby1");
        sb.append("      , h.hobby2");
        sb.append("      , h.hobby3");
        sb.append("      , h.hobby4");
        sb.append("      , h.hobby5");
        sb.append("    ) ");
        sb.append("    OR ? IN ( ");
        sb.append("      h.hobby1");
        sb.append("      , h.hobby2");
        sb.append("      , h.hobby3");
        sb.append("      , h.hobby4");
        sb.append("      , h.hobby5");
        sb.append("    ) ");
        sb.append("    OR ? IN ( ");
        sb.append("      h.hobby1");
        sb.append("      , h.hobby2");
        sb.append("      , h.hobby3");
        sb.append("      , h.hobby4");
        sb.append("      , h.hobby5");
        sb.append("    )");
        sb.append("  )");
        RowMapper<UserMaster> mapper = new BeanPropertyRowMapper<UserMaster>(UserMaster.class);
        return jdbcTemplate.query(sb.toString(),
                mapper,
                targetUserMaster.getId(),
                targetUserMaster.getBloodType(),
                targetUserMaster.getHobby1(),
                targetUserMaster.getHobby2(),
                targetUserMaster.getHobby3(),
                targetUserMaster.getHobby4(),
                targetUserMaster.getHobby5());
    }
    
}
