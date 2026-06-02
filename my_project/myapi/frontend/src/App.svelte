<script>
    import Router from 'svelte-spa-router'
    import Home from "./routes/home.svelte"

    const routes = {
        '/' : Home,
    }
</script>

<Router {routes}/>