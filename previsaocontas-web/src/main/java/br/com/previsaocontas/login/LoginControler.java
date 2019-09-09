package br.com.previsaocontas.login;

import java.io.Serializable;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;

import br.com.previsaocontas.dao.UsuarioDAOImpl;
import br.com.previsaocontas.model.Usuario;

@ManagedBean
@ViewScoped
public class LoginControler implements Serializable {

	private static final long serialVersionUID = 1L;

	private Usuario usuario;

	@ManagedProperty(value = "#{usuarioDAOImpl}")
	private UsuarioDAOImpl usuarioDAOImpl;

	public UsuarioDAOImpl getUsuarioDAOImpl() {
		return usuarioDAOImpl;
	}

	public void setUsuarioDAOImpl(UsuarioDAOImpl usuarioDAOImpl) {
		this.usuarioDAOImpl = usuarioDAOImpl;
	}

	@PostConstruct
	public void iniciar() {
		this.setUsuario(new Usuario());
	}

	public Usuario getUsuario() {

		HttpSession session = SessionBean.getSession();

		if (this.usuario == null && (Usuario) session.getAttribute("usuario") == null) {
			this.setUsuario(new Usuario());
		}

		if ((Usuario) session.getAttribute("usuario") != null) {
			this.setUsuario((Usuario) session.getAttribute("usuario"));
		}

		return usuario;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}

	// validate login
	public void validaLogin() {

		try {
			this.setUsuario(usuarioDAOImpl.buscaUsuario(this.getUsuario().getLogin(), this.getUsuario().getSenha()));
			if (this.getUsuario().getId() != null) {
				HttpSession session = SessionBean.getSession();
				session.setAttribute("usuario", this.getUsuario());
			} else {
				FacesContext.getCurrentInstance().addMessage(null,
						new FacesMessage(FacesMessage.SEVERITY_WARN, "Usuário ou Senha Inválidos!", null));
				System.out.println("Usuário ou Senha Inválidos!");
			}
		} catch (Exception e) {
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getMessage(), null));
			e.printStackTrace();
		}

	}

	// logout event, invalidate session
	public String logout() {
		HttpSession session = SessionBean.getSession();
		session.invalidate();
		// session.setAttribute("usuario", null);
		return "/login.xhtml?faces-redirect=true";

	}

}
