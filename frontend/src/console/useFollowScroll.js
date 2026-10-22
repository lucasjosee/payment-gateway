import { useLayoutEffect, useRef } from 'react'

const FOLLOW_THRESHOLD_PX = 32

// Acompanha o fim da lista, a não ser que a pessoa tenha rolado para cima para ler alguma coisa.
export function useFollowScroll() {
  const ref = useRef(null)
  const followingRef = useRef(true)

  useLayoutEffect(() => {
    if (followingRef.current) {
      ref.current.scrollTop = ref.current.scrollHeight
    }
  })

  function handleScroll() {
    const list = ref.current
    followingRef.current = list.scrollHeight - list.scrollTop - list.clientHeight < FOLLOW_THRESHOLD_PX
  }

  return { ref, onScroll: handleScroll }
}
