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
//    → Aqui você enviará o e-mail pelo seu serviço SMTP / SendGrid / Resend
// -------------------------------------------------------
exports.onMailRequest = onDocumentCreated("mail/{docId}", async (event) => {
  const data = event.data.data();

  console.log("📩 Novo e-mail solicitado:", data);

  // Aqui você implementa o envio real do e-mail
  return null;
});

// -------------------------------------------------------
// 2) SALVAR SENHA DO RESPONSÁVEL (Cloud Function onCall)
//    → Recebe UID do usuário + senha (já validada via código)
// -------------------------------------------------------
exports.saveResponsiblePassword = onCall(async (request) => {
  const uid = request.data.uid; // UID do usuário recebido do app
  const password = request.data.password;

  if (!uid || !password) {
    throw new HttpsError("invalid-argument", "UID ou senha não fornecidos.");
  }

  console.log("Salvando senha de responsável para UID:", uid);

  const hashedPassword = await bcrypt.hash(password, 10);

  await db
    .collection("usuarios")
    .doc(uid)
    .set({ senha_responsavel: hashedPassword }, { merge: true });

  return { success: true, message: "Senha cadastrada com sucesso!" };
});

// -------------------------------------------------------
// 3) VERIFICAR SENHA DO RESPONSÁVEL (Cloud Function onCall)
//    → Recebe UID do usuário + senha digitada
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
    return { success: false, message: "Senha incorreta" };
  }

  return { success: true, message: "Senha correta!" };
});
