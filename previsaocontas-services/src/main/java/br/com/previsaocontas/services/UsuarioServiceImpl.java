package br.com.previsaocontas.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.previsaocontas.dao.UsuarioDAOImpl;
import br.com.previsaocontas.model.Usuario;

@Service
@Transactional(rollbackFor = Throwable.class)
public class UsuarioServiceImpl {

    @Autowired
    private UsuarioDAOImpl usuarioDAOImpl;

    public void salvar(Usuario usuario) throws Exception {
	usuarioDAOImpl.salvar(usuario);
    }

    public void buscaUsuario(String usuario, String senha) throws Exception {
	usuarioDAOImpl.buscaUsuario(usuario, senha);
    }

    public Usuario obter(Long id) throws Exception {
	return usuarioDAOImpl.obter(id);
    }

}
