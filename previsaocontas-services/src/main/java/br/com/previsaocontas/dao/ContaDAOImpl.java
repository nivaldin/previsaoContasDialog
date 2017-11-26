package br.com.previsaocontas.dao;

import java.util.Calendar;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;

import br.com.previsaocontas.arquitetura.HibernateDAO;
import br.com.previsaocontas.enums.EnumStatusConta;
import br.com.previsaocontas.model.Conta;
import br.com.previsaocontas.model.Usuario;
import br.com.previsaocontas.utilitarios.UtilObjeto;

@Repository
public class ContaDAOImpl extends HibernateDAO<Conta> {

    private Criteria restrictionUsuario(Criteria criteria, Usuario usuario) {
	criteria.add(Restrictions.eq("usuario", usuario));
	return criteria;
    }
    
    public Long obterProximo(Usuario usuario) {

	Criteria criteria = this.novoCriteria();

	criteria.setProjection(Projections.max("numr_agrupador"));
	criteria = this.restrictionUsuario(criteria, usuario);

	Long codigo = (Long) criteria.uniqueResult();

	if (UtilObjeto.isNull(codigo)) {

	    codigo = 0L;
	}

	return codigo + 1;
    }

    @SuppressWarnings("unchecked")
    public List<Conta> buscaContaMes(Integer mesSelecionado, Integer anoSelecionado, Usuario usuario) {

	Criteria criteria = this.novoCriteria();

	Calendar dataI = Calendar.getInstance();
	Calendar dataF = Calendar.getInstance();

	dataI.set(anoSelecionado, mesSelecionado - 1, 1, 0, 0, 0);
	dataF.set(anoSelecionado, mesSelecionado - 1, 1, 23, 59, 59);

	dataF.add(Calendar.MONTH, 1);
	dataF.add(Calendar.DAY_OF_MONTH, -1);

	criteria = this.restrictionUsuario(criteria, usuario);
	criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);

	criteria.add(Restrictions.isNull("contaPai"));
	
	criteria.add(Restrictions.ge("data_mes", dataI.getTime()));
	criteria.add(Restrictions.le("data_mes", dataF.getTime()));

	criteria.addOrder(Order.asc("tipo"));
	criteria.addOrder(Order.asc("flag_comum"));
	criteria.addOrder(Order.desc("data_registro"));

	return criteria.list();
    }

    @SuppressWarnings("unchecked")
    public List<Conta> buscaContasAcumuladoAberto(Integer mesSelecionado, Integer anoSelecionado, Usuario usuario) {

	Criteria criteria = this.novoCriteria();

	Calendar dataF = Calendar.getInstance();
	dataF.set(anoSelecionado, mesSelecionado - 1, 1, 0, 0, 0);

	dataF.add(Calendar.MONTH, 1);
	dataF.add(Calendar.DAY_OF_MONTH, -1);

	criteria = this.restrictionUsuario(criteria, usuario);
	criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
	
	criteria.add(Restrictions.isNull("contaPai"));

	criteria.add(Restrictions.le("data_mes", dataF.getTime()));
	criteria.add(Restrictions.eq("status", EnumStatusConta.A));

	return criteria.list();

    }

    @SuppressWarnings("unchecked")
    public List<Conta> buscaContasAgrupador(Long numr_agrupador, Usuario usuario) {
	Criteria criteria = this.novoCriteria();

	criteria = this.restrictionUsuario(criteria, usuario);

	criteria.add(Restrictions.eq("numr_agrupador", numr_agrupador));

	return criteria.list();
    }

}
