#include "globals.hpp"
#include <iostream>
#include <fstream>
#include <sstream>
#include <vector>
#include <string>

DECLARE_int32(str_iters);


void degree_forwarder(void* object, std::vector<edge_t> edges);

void Globals::print()
{
    LOG(INFO) << "file size: " << FILESIZE;
    LOG(INFO) << "graph file: " << INPUT_FILE_NAME;
    LOG(INFO) << "number of partitions: " << NUM_PARTITIONS;
    LOG(INFO) << "max number of partitions: " << MAX_NUM_PARTITION;
    LOG(INFO) << "prepartitioner: " << PREPARTITIONER;
    LOG(INFO) << "  (lambda: " << LAMBDA << ")";

    if (PREPARTITIONER == "streamcom")
    {
        LOG(INFO) << "  (max community size: " << MAX_COM_VOLUME << ")";
    }
    LOG(INFO) << "number of vertices: " << NUM_VERTICES;
    LOG(INFO) << "number of edges: " << NUM_EDGES;
}

void Globals::read_and_do(void (*f)(void*, std::vector<edge_t>), void* context, std::string op_name)
{
    // if the file has already read by the end, seeking to the new position doesn't work.
    // need to clear the buffer before seeking.
    FIN.clear();
    FIN.seekg(sizeof(NUM_VERTICES) + sizeof(NUM_EDGES), std::ios::beg);

    Timer timer;
    timer.start();
    LOG(INFO) <<"Calculating "<< op_name << " batchsize=(" << FLAGS_memsize << " MB) ...";
    std::vector<edge_t> edges;
    auto num_edges_left = NUM_EDGES;
    for (uint32_t i = 0; i < NUM_BATCHES; i++)
    {
        auto num_edges_per_batch = NUM_EDGES_PER_BATCH < num_edges_left ? NUM_EDGES_PER_BATCH : num_edges_left;
        edges.resize(num_edges_per_batch);
        FIN.read((char *)&edges[0], sizeof(edge_t) * num_edges_per_batch);
        // do function
        f(context, edges);
        num_edges_left -= num_edges_per_batch;
    }
    timer.stop();
    LOG(INFO) << "Runtime for calculating " << op_name <<" [sec]: " << timer.get_time(); 
}

void Globals::do_degree_calculation(std::vector<edge_t> edges)
{
    // if (FLAGS_str_iters != 1)
    // {
    //     std::string degrees_filename = "../Input/dblp_dataset_shuffled/Degrees/comm_vols_degree_results_2psl.csv";
    //     std::ifstream vols_file(degrees_filename);
    //     std::string line;
    //     while (getline(vols_file, line)) {
    //         std::stringstream ss(line);
    //         std::string index, value;
    //         getline(ss, index, ',');
    //         getline(ss, value, ',');
    //         uint32_t comm = stoi(index);
    //         // if (stoi(value) == 0)
    //         //     continue;
    //         DEGREES[comm] = stoi(value);
    //     }
    // }else 
    // {
        for (auto& edge : edges)
        {
            ++DEGREES[edge.first];
            ++DEGREES[edge.second];
        }
    //}
}

void Globals::calculate_degrees()
{
    read_and_do(&degree_forwarder, this, "degrees");
}

DECLARE_double(balance_ratio);
Globals::Globals(std::ifstream &fin, std::string input_file_name, uint32_t p, std::string prepartitioner, double lambda, bool cluster_quality_eval)
        : FIN(std::move(fin)),
        INPUT_FILE_NAME(std::move(input_file_name)),
        NUM_PARTITIONS(p),
        PREPARTITIONER(std::move(prepartitioner)),
        LAMBDA(lambda),
		CLUSTER_QUALITY_EVAL(cluster_quality_eval)
{
    if (p > MAX_NUM_PARTITION) {
        LOG(ERROR) << "The number of partitions (" << p << ") is more than max number of partitions (" << MAX_NUM_PARTITION << ")";
        exit(1);
    }
    FILESIZE = FIN.tellg();
    FIN.seekg(0, std::ios::beg);

    FIN.read((char *)&NUM_VERTICES, sizeof(NUM_VERTICES));
    FIN.read((char *)&NUM_EDGES, sizeof(NUM_EDGES));
    CHECK_EQ(sizeof(vid_t) + sizeof(size_t) + NUM_EDGES * sizeof(edge_t), FILESIZE);

    MAX_PARTITION_LOAD = FLAGS_balance_ratio * NUM_EDGES/NUM_PARTITIONS; // edge load
    MAX_COM_VOLUME = 2 * NUM_EDGES/NUM_PARTITIONS;

    NUM_BATCHES = (FILESIZE/(FLAGS_memsize * 1024 * 1024)) + 1;
    NUM_EDGES_PER_BATCH = (NUM_EDGES/NUM_BATCHES) + 1;

    DEGREES.resize(NUM_VERTICES, 0);

    print(); 
}

void degree_forwarder(void* object, std::vector<edge_t> edges)
{
    static_cast<Globals*>(object)->do_degree_calculation(edges);
}
