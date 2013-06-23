package db.service;

import java.io.Serializable;

import org.apache.log4j.Logger;
import org.hibernate.EmptyInterceptor;
import org.hibernate.type.Type;

import world.World;

import db.model.DataBaseEntry;

/**
 * @author liuzg Hibernate全局拦截器
 */
public class HibernateInterceptor extends EmptyInterceptor {
	private static Logger logger = Logger.getLogger(HibernateInterceptor.class);
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public boolean onLoad(Object entity, Serializable id, Object[] state,
			String[] propertyNames, Type[] types) {
		if (HibernateUtil.sessionOpenTimes >= Long.MAX_VALUE - 10) {
			HibernateUtil.sessionOpenTimes = 0;
		}
		HibernateUtil.sessionOpenTimes++;
		if (entity instanceof DataBaseEntry) {
			if (World.isPrintDetailInfo) {
				logger.info("加载对象实体:" + entity.getClass().toString());
				String str = "id=" + id;
				for (int index = 0; index < state.length; index++) {
					str += "," + propertyNames[index] + "=" + state[index];
				}
				logger.info(str);
			}
		}
		return super.onLoad(entity, id, state, propertyNames, types);
	}

	@Override
	public boolean onSave(Object entity, Serializable id, Object[] state,
			String[] propertyNames, Type[] types) {
		if (entity instanceof DataBaseEntry) {
			if (World.isPrintDetailInfo) {
				logger.info("保存对象实体:" + entity.getClass().toString());
				String str = "id=" + id;
				for (int index = 0; index < state.length; index++) {
					str += "," + propertyNames[index] + "=" + state[index];
				}
				logger.info(str);
			}
		}
		return super.onSave(entity, id, state, propertyNames, types);
	}

	@Override
	public void onDelete(Object entity, Serializable id, Object[] state,
			String[] propertyNames, Type[] types) {
		if (entity instanceof DataBaseEntry) {
			if (World.isPrintDetailInfo) {
				logger.info("删除对象实体:" + entity.getClass().toString());
				String str = "id=" + id;
				for (int index = 0; index < state.length; index++) {
					str += "," + propertyNames[index] + "=" + state[index];
				}
				logger.info(str);
			}
		}
		super.onDelete(entity, id, state, propertyNames, types);
	}

	@Override
	public boolean onFlushDirty(Object entity, Serializable id,
			Object[] currentState, Object[] previousState,
			String[] propertyNames, Type[] types) {
		if (entity instanceof DataBaseEntry) {
			if (World.isPrintDetailInfo) {
				logger.info("更新对象实体:" + entity.getClass().toString());
				String str = "id=" + id;
				for (int index = 0; index < currentState.length; index++) {
					str += "," + propertyNames[index] + "="
							+ currentState[index];
				}
				logger.info(str);
			}
		}
		return super.onFlushDirty(entity, id, currentState, previousState,
				propertyNames, types);
	}

}
