package br.com.leonardo.meuifood.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;

import br.com.leonardo.meuifood.R;
import br.com.leonardo.meuifood.helper.ConfiguracaoFirebase;
import br.com.leonardo.meuifood.helper.UsuarioFirebase;

public class AutenticacaoActivity extends AppCompatActivity {

    private Button botaoAcessar;
    private EditText campoEmail, campoSenha;
    private Switch tipoAcesso, tipoUsuario;
    private LinearLayout linearTipoUsuario;

    private FirebaseAuth autenticacao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_autenticacao);


        inicializarComponentes();
        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        autenticacao.signOut();
        
        //Verifica usuario logado
        verificaUsuarioLogado();
        tipoAcesso.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){//é empresa
                    linearTipoUsuario.setVisibility(View.VISIBLE);
                }else {//é usuario
                    linearTipoUsuario.setVisibility(View.GONE);
                }
            }
        });

        botaoAcessar.setOnClickListener(v -> {
            String email = campoEmail.getText().toString();
            String senha = campoSenha.getText().toString();

            if (!email.isEmpty()) {
                if (!senha.isEmpty()) {

                    //verifica o estado do switch
                    if (tipoAcesso.isChecked()) {//cadastro
                        autenticacao.createUserWithEmailAndPassword(email, senha).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {

                                Toast.makeText(AutenticacaoActivity.this, "Cadastro realizado com sucesso", Toast.LENGTH_SHORT).show();

                                String tipoUsuario = getTipoUsuario();
                                UsuarioFirebase.atualizarTipoUsuario(tipoUsuario);
                               abrirTelaPrincipal(tipoUsuario);

                            } else {

                                String erroExcecao = "";
                                try {
                                    throw task.getException();
                                } catch (FirebaseAuthWeakPasswordException e) {
                                    erroExcecao = "Digite uma Senha mais forte!";
                                } catch (FirebaseAuthInvalidCredentialsException e) {
                                    erroExcecao = "por favor, digite um e-mail válido!";
                                } catch (FirebaseAuthUserCollisionException e) {
                                    erroExcecao = "Essa conta já foi cadastrada";
                                } catch (Exception e) {
                                    erroExcecao = "Erro ao cadstrar usuário" + e.getMessage();
                                    e.printStackTrace();
                                }

                                Toast.makeText(AutenticacaoActivity.this, "Erro" + erroExcecao, Toast.LENGTH_SHORT).show();
                            }

                        });
                    } else {//login

                        autenticacao.signInWithEmailAndPassword(email, senha).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()){

                                    Toast.makeText(AutenticacaoActivity.this, "Logado com sucesso", Toast.LENGTH_SHORT).show();
                                    String tipoUsuario = task.getResult().getUser().getDisplayName();
                                    abrirTelaPrincipal(tipoUsuario);

                                }else {
                                    Toast.makeText(AutenticacaoActivity.this, "Erro fazer login: " + task.getException(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                    }
                } else {
                    Toast.makeText(AutenticacaoActivity.this, "Preencha a senha", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(AutenticacaoActivity.this, "Preencha o email!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void verificaUsuarioLogado(){
        FirebaseUser usuarioAtual = autenticacao.getCurrentUser();
        if( usuarioAtual != null){
            String tipoUsuario = usuarioAtual.getDisplayName();
            abrirTelaPrincipal(tipoUsuario);
        }
    }

    private String getTipoUsuario(){
        return tipoUsuario.isChecked() ? "Empresa" : "Usuario";
    }

    private void abrirTelaPrincipal(String tipoUsuario){

        if (tipoUsuario.equals("Empresa")){
            startActivity(new Intent(getApplicationContext(), EmpresaActivity.class));

        }else {
            startActivity(new Intent(getApplicationContext(), HomeActivity.class));
        }
    }

    private void inicializarComponentes() {

        campoEmail = findViewById(R.id.editUsuarioNome);
        campoSenha = findViewById(R.id.editEntregaTempo);
        botaoAcessar = findViewById(R.id.buttonAcesso);
        tipoAcesso = findViewById(R.id.switchAcesso);
        tipoUsuario = findViewById(R.id.switchTipoUsuario);
        linearTipoUsuario = findViewById(R.id.linearTipoUsuario);


    }
}