package br.com.hospitalesperanca.sicc.dto;

import br.com.hospitalesperanca.sicc.model.Perfil;
import br.com.hospitalesperanca.sicc.model.Usuario;

/** Usuário devolvido pela API - nunca inclui a senha. Campos iguais ao UsuarioAutenticado do auth.ts. */
public record UsuarioResponse(
        Long id,
        String nome,
        String email,
        String matricula,
        Perfil perfil,
        boolean ativo,
        String iniciais) {

    public static UsuarioResponse de(Usuario usuario) {
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getMatricula(),
                usuario.getPerfil(),
                usuario.isAtivo(),
                iniciais(usuario.getNome()));
    }

    /** "Cristian Freitas" -> "CF". */
    private static String iniciais(String nome) {
        if (nome == null || nome.isBlank()) {
            return "";
        }
        String[] partes = nome.trim().split("\\s+");
        String primeira = partes[0].substring(0, 1);
        String ultima = partes.length > 1 ? partes[partes.length - 1].substring(0, 1) : "";
        return (primeira + ultima).toUpperCase();
    }
}
