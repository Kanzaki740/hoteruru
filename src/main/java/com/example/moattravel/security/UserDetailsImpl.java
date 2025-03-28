package com.example.moattravel.security;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.example.moattravel.entity.User;

public class UserDetailsImpl implements UserDetails {
	private final User user;
	private final Collection<GrantedAuthority> authorities;

	public UserDetailsImpl(User user, Collection<GrantedAuthority> authorities) {
		this.user = user;
		this.authorities = authorities;
	}

	public User getUser() {
		return user;
	}

	//ハッシュ化済みのパスワードを返す
	@Override
	public String getPassword() {
		return user.getPassword();
	}

	//ログイン時に利用するユーザ名(メールアドレス)を返す
	@Override
	public String getUsername() {
		return user.getEmail();
	}

	//ロールのコレクションを返す
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return authorities;
	}

	//アカウントが期限切れでなければtrueを返す
	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	//ユーザがロックされていなければtrueを返す
	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	//ユーザのパスワードが期限切れでなければtrueを返す
	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	//ユーザが有効であればtrueを返す
	@Override
	public boolean isEnabled() {
		return user.getEnabled();
	}
}
