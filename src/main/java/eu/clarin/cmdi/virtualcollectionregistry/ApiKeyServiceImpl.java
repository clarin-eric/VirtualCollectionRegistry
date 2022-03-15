package eu.clarin.cmdi.virtualcollectionregistry;

import eu.clarin.cmdi.virtualcollectionregistry.model.ApiKey;
import eu.clarin.cmdi.virtualcollectionregistry.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import java.security.Principal;
import java.util.Date;

@Component
public class ApiKeyServiceImpl extends TxManager implements ApiKeyService {
    private final static Logger logger = LoggerFactory.getLogger(ApiKeyServiceImpl.class);

    @Autowired
    private VirtualCollectionRegistry registry;

    private final ApiKeyGenerator generator = new ApiKeyGenerator();

    private final DataStore datastore;

    @Autowired
    public ApiKeyServiceImpl(DataStore datastore) {
        this.datastore = datastore;
    }

    @Override
    public void generateNewKeyForUser(String username) {
        EntityManager em = datastore.getEntityManager();
        try {
            em.getTransaction().begin();
            //Query user
            TypedQuery<User> q
                    = em.createNamedQuery("User.findByName", User.class);
            q.setParameter("name", username);
            User user = q.getSingleResult();

            if(user == null) {

            }
            //Generate new api key and associate with the user
            ApiKey key = new ApiKey();
            key.setValue(generator.newToken());
            key.setUser(user);
            key.setCreatedAt(new Date());
            user.getApiKeys().add(key);

            em.getTransaction().commit();

            logger.info("Generated token="+key.getValue()+" for user="+username);

        } catch (Exception e) {
            em.getTransaction().rollback();
            logger.error("error while querying user with name="+username, e);
        }
    }

    @Override
    public void revokeKey(String key) {
        logger.info("Revoking key="+key);
        EntityManager em = datastore.getEntityManager();
        try {
            em.getTransaction().begin();

            //Query apiKey
            TypedQuery<ApiKey> q = em.createNamedQuery("ApiKey.findByValue", ApiKey.class);
            q.setParameter("value", key);
            ApiKey apiKey = q.getSingleResult();

            if(apiKey.getRevokedAt() == null) {
                //Update
                apiKey.setRevokedAt(new Date());
                logger.debug("Updated api key (" + key + ") with new revoked at date.");
            }

            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            logger.error("error while querying user for api key="+key, e);
        }
    }

    @Override
    public User getUserForApiKey(String key) throws ApiKeyNotFoundException, ApiKeyRevokedException, ApiKeyException {
        User user = null;
        EntityManager em = datastore.getEntityManager();
        try {
            em.getTransaction().begin();

            //Query apiKey
            TypedQuery<ApiKey> q = em.createNamedQuery("ApiKey.findByValue", ApiKey.class);
            q.setParameter("value", key);
            ApiKey apiKey = q.getSingleResult();

            if(apiKey.getRevokedAt() != null) {
                throw new ApiKeyRevokedException(key);
            }
            //Update
            apiKey.setLastUsedAt(new Date());
            user = apiKey.getUser();

            logger.debug("Updated api key ("+key+") with new last used date.");
            em.getTransaction().commit();
        } catch (NoResultException e) {
            em.getTransaction().rollback();
            throw new ApiKeyNotFoundException(key);
        } catch (Exception e) {
            em.getTransaction().rollback();
            logger.error("error while querying user for api key="+key, e);
        }
        return user;
    }
}
