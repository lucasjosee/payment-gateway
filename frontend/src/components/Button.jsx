import styles from './Button.module.css'

// O type padrão é "button" porque, dentro de um <form>, um botão sem type envia o formulário. Só o botão que
// envia de propósito deve declarar type="submit".
function Button({ variant = 'primary', size = 'md', type = 'button', className, ...props }) {
  const classes = [styles.button, styles[variant], styles[size], className].filter(Boolean).join(' ')
  return <button type={type} className={classes} {...props} />
}

export default Button
