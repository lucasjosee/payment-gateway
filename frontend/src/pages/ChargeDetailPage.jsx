import { useParams } from 'react-router'

function ChargeDetailPage() {
  const { id } = useParams()
  return <h1>Cobrança {id}</h1>
}

export default ChargeDetailPage
