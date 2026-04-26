// -------------------------------------------------------
// Firebase Functions v2 + Firestore + E-mail Trigger
// -------------------------------------------------------
const { onCall, HttpsError } = require("firebase-functions/v2/https");
const { onDocumentCreated } = require("firebase-functions/v2/firestore");
const admin = require("firebase-admin");
const bcrypt = require("bcryptjs");

admin.initializeApp();
const db = admin.firestore();

// -------------------------------------------------------
// 1) TRIGGER: Quando documento é criado na collection "mail"
// -------------------------------------------------------
exports.onMailRequest = onDocumentCreated("mail/{docId}", async (event) => {
  const data = event.data.data();

  console.log("📩 Novo e-mail solicitado:", data);

  // Aqui você implementaria envio manual de e-mail caso não use a extensão
  // Firebase Trigger Email

  return null;
});

// -------------------------------------------------------
// 2) SALVAR SENHA DO RESPONSÁVEL
// -------------------------------------------------------
exports.saveResponsiblePassword = onCall(async (request) => {

  const uid = request.data.uid;
  const password = request.data.password;

  if (!uid || !password) {
    throw new HttpsError("invalid-argument", "UID ou senha não fornecidos.");
  }

  console.log("Salvando senha de responsável para UID:", uid);

  const hashedPassword = await bcrypt.hash(password, 10);

  await db
    .collection("usuarios")
    .doc(uid)
    .set(
      { senha_responsavel: hashedPassword },
      { merge: true }
    );

  return {
    success: true,
    message: "Senha cadastrada com sucesso!"
  };
});


// -------------------------------------------------------
// 3) VERIFICAR SENHA DO RESPONSÁVEL
// -------------------------------------------------------

exports.verifyResponsiblePassword = onCall(async (request) => {

  const uid = request.data.uid;
  const password = request.data.password;

  if (!uid || !password) {
    throw new HttpsError("invalid-argument", "UID ou senha não fornecidos.");
  }

  console.log("🔍 Verificando senha de responsável para UID:", uid);

  const doc = await db.collection("usuarios").doc(uid).get();

  if (!doc.exists || !doc.get("senha_responsavel")) {
    throw new HttpsError("not-found", "Senha não cadastrada para este usuário.");
  }

  const storedHash = doc.get("senha_responsavel");

  const isValid = await bcrypt.compare(password, storedHash);

  if (!isValid) {
    return {
      success: false,
      message: "Senha incorreta"
    };
  }

  return {
    success: true,
    message: "Senha correta!"
  };
});


// -------------------------------------------------------
// 4) ENVIAR CÓDIGO DE VERIFICAÇÃO POR E-MAIL
// -------------------------------------------------------

exports.enviarCodigo = onCall(async (request) => {

  const email = request.data.email?.toLowerCase().trim();

  if (!email) {
    throw new HttpsError("invalid-argument", "Email não fornecido.");
  }

  const codigoRef = db.collection("codigos_email").doc(email);
  const existing = await codigoRef.get();

  // Proteção simples contra spam
  if (existing.exists) {

    const data = existing.data();

    if (data.expireAt && Date.now() < data.expireAt - (9 * 60 * 1000)) {
      throw new HttpsError(
        "resource-exhausted",
        "Aguarde antes de solicitar outro código."
      );
    }
  }

  // gera código de 6 dígitos
  const codigo = Math.floor(100000 + Math.random() * 900000).toString();

  const codigoHash = await bcrypt.hash(codigo, 10);

  const expireAt = Date.now() + 10 * 60 * 1000; // 10 minutos

  await codigoRef.set({
    codigo_hash: codigoHash,
    expireAt: expireAt
  });

  // cria documento para disparar email
  await db.collection("mail").add({

    to: email,

    message: {
      subject: "Seu código de verificação",
      text: `Seu código de verificação é: ${codigo}`
    }

  });

  console.log("📨 Código enviado para:", email);

  return { success: true };
});


// -------------------------------------------------------
// 5) VALIDAR CÓDIGO DE VERIFICAÇÃO
// -------------------------------------------------------

exports.validarCodigo = onCall(async (request) => {

  const email = request.data.email?.toLowerCase().trim();
  const codigo = request.data.codigo;

  if (!email || !codigo) {
    throw new HttpsError("invalid-argument", "Dados incompletos.");
  }

  const doc = await db.collection("codigos_email").doc(email).get();

  if (!doc.exists) {
    throw new HttpsError("not-found", "Código não encontrado.");
  }

  const data = doc.data();

  if (!data.expireAt || Date.now() > data.expireAt) {
    throw new HttpsError("deadline-exceeded", "Código expirado.");
  }

  const valido = await bcrypt.compare(codigo, data.codigo_hash);

  if (!valido) {
    throw new HttpsError("permission-denied", "Código inválido.");
  }

  // remove código após uso
  await db.collection("codigos_email").doc(email).delete();

  console.log("✅ Código validado com sucesso para:", email);

  return { success: true };
});