package br.com.previsaocontas.dao;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;

import br.com.previsaocontas.arquitetura.HibernateDAO;
import br.com.previsaocontas.model.Usuario;
import br.com.previsaocontas.utilitarios.UtilSecurity;

@Repository
public class UsuarioDAOImpl extends HibernateDAO<Usuario> {

    public Usuario buscaUsuario(String usuario, String senha) throws Exception {

	final Criteria criteria = this.novoCriteria();

	criteria.add(Restrictions.eq("login", usuario));
	criteria.add(Restrictions.eq("senha", UtilSecurity.cript(senha)));

	return (Usuario) criteria.uniqueResult();
    }
    

}
