/*
 * Copyright 2016 Robert Li.
 * Released under the MIT license
 * https://opensource.org/licenses/MIT
 */
package robertli.zero.dao.impl;

import java.util.List;
import javax.annotation.Resource;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Component;
import robertli.zero.dao.UserDao;
import robertli.zero.entity.User;
import robertli.zero.model.SearchResult;

/**
 *
 * @author Robert Li
 */
@Component("userDao")
public class UserDaoImpl extends GenericHibernateDao<User, Integer> implements UserDao {

    @Resource
    private SessionFactory sessionFactory;

    @Override
    public User saveUser(String name, String password, String passwordSalt) {
        User user = new User();
        user.setName(name);
        user.setPassword(password);
        user.setPasswordSalt(passwordSalt);
        save(user);
        return user;
    }

    @Override
    public SearchResult<User> paging(int pageId, int max) {
        SearchResult<User> result = query("from User", pageId, max);
        for (User user : result.getList()) {
            user.getUserAuthList().size();//fetch UserAuth
        }
        return result;
    }

    @Override
    public SearchResult<User> searchByName(String keyword, int pageId, int max) {
        Session session = sessionFactory.getCurrentSession();
        keyword = "%" + keyword + "%";
        Query countQuery = session.createQuery("select count(*) from User where name like :keyword");
        countQuery.setString("keyword", keyword);
        Number number = (Number) countQuery.uniqueResult();
        int count = number.intValue();
        int start = (pageId - 1) * max;
        Query query = session.createQuery("from User where name like :keyword");
        query.setString("keyword", keyword);
        query.setFirstResult(start);
        query.setMaxResults(max);
        List<User> list = query.list();
        for (User user : list) {
            user.getUserAuthList().size();//fetch UserAuth
        }
        return new SearchResult<>(start, max, count, list);
    }

    @Override
    public SearchResult<User> searchByAuthId(String authId, int pageId, int max) {
        Session session = sessionFactory.getCurrentSession();
        authId = "%" + authId + "%";
        Query countQuery = session.createQuery("select count(*) from UserAuth as ua where ua.authId like :authId");
        countQuery.setString("authId", authId);
        Number number = (Number) countQuery.uniqueResult();
        int count = number.intValue();
        int start = (pageId - 1) * max;
        Query query = session.createQuery("select ua.user from UserAuth as ua where ua.authId like :authId");
        query.setString("authId", authId);
        query.setFirstResult(start);
        query.setMaxResults(max);
        List<User> list = query.list();
        for (User user : list) {
            user.getUserAuthList().size();//fetch UserAuth
        }
        return new SearchResult<>(start, max, count, list);
    }

}
