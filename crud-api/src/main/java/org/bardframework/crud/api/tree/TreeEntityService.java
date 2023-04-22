package org.bardframework.crud.api.tree;

import org.bardframework.crud.api.base.BaseCriteria;
import org.bardframework.crud.api.base.BaseModel;
import org.bardframework.crud.api.base.BaseRepository;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by vahid (va.zafari@gmail.com) on 11/12/17.
 */
public interface TreeEntityService<M extends BaseModel<I> & TreeEntityModel<M>, C extends BaseCriteria<I> & TreeEntityCriteria<I>, R extends BaseRepository<M, C, I, U> & TreeEntityRepository<M, I, U>, I, U> {

    M getEmptyModel();

    C getEmptyCriteria();

    R getRepository();

    Logger getLogger();

    default M getTree(U user) {
        return this.toTree(this.getRepository().get(this.getEmptyCriteria(), user));
    }

    default M getTree(I rootId, U user) {
        return this.toTree(this.getRepository().getWithChildren(rootId, user));
    }

    default M toTree(List<M> list) {
        Map<I, M> map = list.stream().collect(Collectors.toMap(M::getId, Function.identity()));
        List<M> roots = new ArrayList<>();
        for (M child : list) {
            if (child.getParent() != null) {
                M parent = map.get(child.getParent().getId());
                if (null == parent) {
                    this.getLogger().warn("can't find parent {}", child.getParent());
                } else {
                    parent.addChild(child);
                }
                child.setParent(null);
            } else {
                roots.add(child);
            }
        }
        if (roots.size() == 1) {
            return roots.get(0);
        }
        M root = this.getEmptyModel();
        root.setChildren(roots);
        return root;
    }

}
