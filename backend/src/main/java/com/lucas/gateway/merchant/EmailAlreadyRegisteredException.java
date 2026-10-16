package com.lucas.gateway.merchant;

import com.lucas.gateway.shared.ConflictException;

public class EmailAlreadyRegisteredException extends ConflictException {

	public EmailAlreadyRegisteredException() {
		super("Já existe um lojista cadastrado com este e-mail.");
	}
}
