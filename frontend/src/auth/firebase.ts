import { initializeApp, type FirebaseApp } from 'firebase/app'
import {
  getAuth,
  GoogleAuthProvider,
  signInWithPopup,
  signOut as firebaseSignOut,
  onAuthStateChanged,
  type Auth,
  type User,
} from 'firebase/auth'

/**
 * Sem VITE_FIREBASE_API_KEY o app roda em modo dev: nao inicializa o Firebase
 * e o backend (perfil dev) resolve o usuario pelo header X-Dev-User.
 * Para usar o login real do Google, preencha o .env — veja o README.
 */
export const MODO_DEV = !import.meta.env.VITE_FIREBASE_API_KEY

export const USUARIO_DEV =
  import.meta.env.VITE_DEV_USER || 'dono@oficinatrackwheel.com.br'

const config = {
  apiKey: import.meta.env.VITE_FIREBASE_API_KEY,
  authDomain: import.meta.env.VITE_FIREBASE_AUTH_DOMAIN,
  projectId: import.meta.env.VITE_FIREBASE_PROJECT_ID,
  storageBucket: import.meta.env.VITE_FIREBASE_STORAGE_BUCKET,
  messagingSenderId: import.meta.env.VITE_FIREBASE_MESSAGING_SENDER_ID,
  appId: import.meta.env.VITE_FIREBASE_APP_ID,
}

let app: FirebaseApp | undefined
let auth: Auth | undefined

if (!MODO_DEV) {
  app = initializeApp(config)
  auth = getAuth(app)
}

export function obterAuth(): Auth {
  if (!auth) {
    throw new Error('Firebase nao inicializado: rodando em modo dev')
  }
  return auth
}

export async function entrarComGoogle(): Promise<User> {
  const provider = new GoogleAuthProvider()
  provider.setCustomParameters({ prompt: 'select_account' })
  const resultado = await signInWithPopup(obterAuth(), provider)
  return resultado.user
}

export async function sair(): Promise<void> {
  if (MODO_DEV) return
  await firebaseSignOut(obterAuth())
}

/** O SDK renova o token sozinho; aqui so pegamos o vigente. */
export async function obterTokenAtual(): Promise<string | null> {
  if (MODO_DEV) return null
  const usuario = obterAuth().currentUser
  return usuario ? usuario.getIdToken() : null
}

export function observarAuth(callback: (usuario: User | null) => void): () => void {
  if (MODO_DEV) {
    callback(null)
    return () => {}
  }
  return onAuthStateChanged(obterAuth(), callback)
}
