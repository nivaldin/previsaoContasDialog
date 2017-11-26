package br.com.previsaocontas.exception;

public class DadosInvalidosException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public DadosInvalidosException(String msg) {
	super(msg);
    }

    public DadosInvalidosException(String msg, Throwable cause) {
	super(msg, cause);
    }

}
