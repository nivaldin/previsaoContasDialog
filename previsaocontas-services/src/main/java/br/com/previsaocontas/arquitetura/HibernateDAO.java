package br.com.previsaocontas.arquitetura;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.Example;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;

import br.com.previsaocontas.utilitarios.UtilColecao;
import br.com.previsaocontas.utilitarios.UtilObjeto;
import br.com.previsaocontas.utilitarios.UtilString;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

@SuppressWarnings({ "unchecked", "restriction" })
public abstract class HibernateDAO<E extends Entidade> {

	public HibernateDAO() {
	}

	@PersistenceContext
	private EntityManager em;


	public E salvar(E entidade) {

		if (!UtilObjeto.isNull(entidade)) {

			if (UtilObjeto.isNull(entidade.getId())) {

				this.em.persist(entidade);
			} else {

				this.em.merge(entidade);

			}

		}

		return entidade;
	}

	public void remover(final E entidade) {

		if (!UtilObjeto.isNull(entidade)) {

			this.em.remove(this.em.getReference(entidade.getClass(), entidade.getId()));

		}
	}

	public void removerTodos(final Collection<E> entidades) {

		if (!UtilObjeto.isNull(entidades)) {

			for (final E entidade : entidades) {

				this.remover(entidade);
			}
		}
	}

	public E obter(Long identificador) {
	    	
		E resultado = null;

		if (!UtilObjeto.isNull(identificador)) {

			final Class<E> tipo = this.getEntidadeClass();

			resultado = this.em.find(tipo, identificador);
			
		}
		
		return resultado;
	}

	public Collection<E> obterPorAtributo(final String atributo, final Serializable valor) {

		final Criteria criteria = this.novoCriteria();

		criteria.add(Restrictions.eq(atributo, valor));

		return criteria.list();
	}

	protected E obter(final Criteria criteria) {

		final List<E> result = criteria.list();

		return UtilColecao.isEmpty(result) ? null : this.getEntidadeClass().cast(result.get(0));
	}

	public Collection<E> consultar(final E entidade) {

		final Criteria criteria = this.novoCriteria();

		if (!UtilObjeto.isNull(entidade)) {

			final Example example = Example.create(entidade);

			example.enableLike(MatchMode.ANYWHERE);

			example.ignoreCase();

			example.excludeZeroes();

			criteria.add(example);
		}

		return this.consultar(criteria);
	}

	protected Collection<E> consultar(final Criteria criteria) {

		criteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);

		return criteria.list();
	}

	public Collection<E> listar() {

		final Criteria criteria = this.novoCriteria();

		criteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);

		return criteria.list();
	}

	protected void verificarFiltro(final Criteria criteria, final Map<String, Object> filtros) {

		for (final String chave : filtros.keySet()) {

			if (UtilObjeto.isNotNull(filtros.get(chave))) {

				final String[] subClass = chave.split(Pattern.quote("."));

				if (subClass.length > 1) {

					criteria.createAlias(subClass[0], subClass[0]);

					this.montarFieldsCriteria(criteria, chave, filtros.get(chave));

					criteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);

				} else {

					this.montarFieldsCriteria(criteria, chave, filtros.get(chave));
				}
			}

		}

	}

	private void montarFieldsCriteria(final Criteria criteria, final String chave, final Object value) {

		if (value instanceof String) {

			if (!UtilString.isEmpty(value.toString())) {

				criteria.add(Restrictions.ilike(chave, value.toString(), MatchMode.ANYWHERE));
			}
		} else {

			criteria.add(Restrictions.eqOrIsNull(chave, value));
		}
	}

	public Criteria listarPaginado(final E entidade) {

		return this.novoCriteria();
	}

	protected boolean isPersistente(final E entidade) {

		return this.em.contains(entidade);
	}

	protected Class<E> getEntidadeClass() {

		final Type type[] = ((ParameterizedTypeImpl) this.getClass().getGenericSuperclass()).getActualTypeArguments();

		return (Class<E>) type[0];
	}

	protected Criteria novoCriteria() {

		final Class<E> clazz = this.getEntidadeClass();

		return ((Session) em.getDelegate()).createCriteria(clazz);
	}

}
