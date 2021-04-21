#dependencies global_script.R, node_edge_lists.R, ratios.R

#install.packages("igraph")

library(igraph)

#prepare edges and node data, create igraph object

edges_igraph <- as.data.frame(edges)
nodes_igraph <- as.data.frame(nodes)
graph_igraph <- graph_from_data_frame(edges_igraph, directed = TRUE, vertices = NULL)

#closeness measure

closeness <- as.data.frame(closeness(graph_igraph, mode="all"))
closeness <- closeness%>%rownames_to_column()%>%
  rename(id = "rowname")

closeness$id <- as.numeric(closeness$id)


closeness_nodes <- full_join(nodes, closeness)

#overall closeness measures

nodes_overall_closeness <- bigrams%>%unnest_tokens(words, text, token="words", drop=FALSE)%>%
  group_by(words)%>%
  count(words,sort=TRUE)%>%
  ungroup()%>%
  mutate(nodes_total=sum(n), nodes_unique = n())%>%
  mutate(bigrams_unique=bigrams_overall[[1,4]])%>%
  mutate(bigrams_total=bigrams_overall[[1,5]])

nodes_overall_closeness <- inner_join(nodes_overall_closeness, closeness_nodes, by=c("words"="label"))

#write_csv(nodes_overall_closeness,"overall_closeness.csv")

#individual closeness measures

nodes_individual_closeness <- bigrams%>%unnest_tokens(words, text, token="words", drop=FALSE)%>%
  group_by(source_id, words)%>%
  count(words, sort=TRUE)%>%
  ungroup()%>%
  group_by(source_id)%>%
  mutate(nodes_total=sum(n), nodes_unique=n())%>%
  rename(label=words)%>%
  ungroup()

nodes_individual_closeness <- full_join(nodes_individual_closeness, closeness_nodes)

#write_csv(nodes_individual_closeness,"individual_closeness.csv")

#edge density overall and by individual

overall_density <- edge_density(graph_igraph, loops = TRUE)

individual_edges_density <- nodes_individual_ratios%>%
  group_by(source_id)%>%
  mutate(edge_density = bigrams_unique/(nodes_unique*(nodes_unique-1)))%>%
  ungroup()

#write_csv(individual_edges_density,"individual_edges_density.csv")

#diameter overall

diameter_overall <- diameter(graph_igraph, directed=FALSE)

#diameter grouped by individual

#function so that diameter can be applied to invididuals inside the dataframe

makemetrics <- function(gr){
  data.frame(diameter(gr))
}

dsplit <- split(edges_individual, edges_individual$source_id)

dsplit <- lapply(dsplit, function(x) makemetrics(graph_from_data_frame(x)))

individual_diameter <- map_df(dsplit, ~.x, .id="source_id")

individual_diameter <- individual_diameter%>%group_by(source_id)%>%
  mutate(diameter_ratio=diameter.gr./diameter_overall)

#prepare dataframe for creation of "massive tibble"

diameter_for_joining <- individual_diameter
diameter_for_joining$source_id <- str_replace_all(diameter_for_joining$source_id, ".txt", "")
diameter_for_joining$source_id <- str_replace_all(diameter_for_joining$source_id, "./", "")
diameter_for_joining$source_id <- as.numeric(diameter_for_joining$source_id)

#write_csv(diameter_for_joining, "diameter_for_joining.csv")
