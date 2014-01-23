package com.alcatel_lucent.dms.service;

import com.alcatel_lucent.dms.BusinessException;
import com.alcatel_lucent.dms.UserContext;
import com.alcatel_lucent.dms.model.PreferredReference;
import com.alcatel_lucent.dms.model.User;
import net.sf.json.JSONObject;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by guoshunw on 14-1-23.
 */

@Service("preferredReferenceService")
public class PreferredReferenceServiceImpl extends BaseServiceImpl implements PreferredReferenceService {

    @Override
    public PreferredReference createPreferredReference(String reference, String comment){
        List<PreferredReference> preferredReferences = dao.retrieve(
                "from PreferredReference where reference = :reference",
                JSONObject.fromObject("{'reference': '" + reference + "'}"));
        if (!preferredReferences.isEmpty()) return null;

        User user = UserContext.getInstance().getUser();

        PreferredReference preferredReference = new PreferredReference();
        preferredReference.setComment(comment);
        preferredReference.setReference(reference);
        preferredReference.setCreator(user);

        preferredReference = (PreferredReference) dao.create(preferredReference);

        return preferredReference;
    }
}
