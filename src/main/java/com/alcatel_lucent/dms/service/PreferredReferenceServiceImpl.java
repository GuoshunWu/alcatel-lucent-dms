package com.alcatel_lucent.dms.service;

import com.alcatel_lucent.dms.UserContext;
import com.alcatel_lucent.dms.model.PreferredReference;
import com.alcatel_lucent.dms.model.User;
import net.sf.json.JSONObject;
import org.intellij.lang.annotations.Language;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by guoshunw on 14-1-23.
 */

@Service("preferredReferenceService")
public class PreferredReferenceServiceImpl extends BaseServiceImpl implements PreferredReferenceService {

    @Override
    public PreferredReference createPreferredReference(String reference, String comment) {
        PreferredReference preferredReferences = (PreferredReference) dao.retrieveOne(
                "from PreferredReference where reference = :reference",
                JSONObject.fromObject("{'reference': '" + reference + "'}"));
        if (null != preferredReferences) {
            // create failed if preference exists and not removed
            if (!preferredReferences.isRemoved()) return null;
            preferredReferences.setRemoved(false);
            return preferredReferences;
        }

        User user = UserContext.getInstance().getUser();

        PreferredReference preferredReference = new PreferredReference();
        preferredReference.setComment(comment);
        preferredReference.setReference(reference);
        preferredReference.setCreator(user);

        preferredReference = (PreferredReference) dao.create(preferredReference);

        return preferredReference;
    }

    @Override
    public PreferredReference updatePreferredReference(Long id, String reference, String comment) {
        PreferredReference preferredReference = (PreferredReference) dao.retrieve(PreferredReference.class, id);
        if (null == preferredReference) return null;
        if (null != reference) {
            preferredReference.setReference(reference);
        }
        if (null != comment) {
            preferredReference.setComment(comment);
        }
        return preferredReference;
    }

    @Override
    public void deletePreferredReferences(Collection<Long> ids) {
        @Language("HQL") String hSQL = "update PreferredReference set removed =:removed where id in :ids";
        Map params = new HashMap();
        params.put("ids", ids);
        params.put("removed", true);
        dao.delete(hSQL, params);
    }
}
