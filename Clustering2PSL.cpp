#include <iostream>
#include <fstream>
#include <string>
#include <sstream>
#include <chrono>
using namespace std;
using namespace std::chrono;

const int NUM_VERTICES = 4000000;
const int NUM_EDGES = 117185084;
const int NUM_PARTITIONS = 4;
const int MAX_COM_VOLUME = 2 * NUM_EDGES/NUM_PARTITIONS;
int DEGREES[NUM_EDGES];

void do_degree_calculation(int u, int v)
{
    ++DEGREES[u];
    ++DEGREES[v];
}

void read()
{
    ifstream fin;
    string line, substr;
    int u, v;
    fin.open("dataset.csv");
    while(!fin.eof())
    {
        getline(fin, line);

        stringstream s(line);
        
        try
        {
            getline(s, substr, ',');
            u = std::stoi(substr);

            getline(s, substr, ',');
            v = std::stoi(substr);
        }
        catch (...)
        {
            cout << "Error in line: " << line;
            break;
        }
        do_degree_calculation(u, v);
    }
    fin.close();
}

void init_degrees() 
{ 
    for (int i = 0; i <= NUM_VERTICES; i++) 
    {
        DEGREES[i] = 0;
    }
}

void print_degrees() 
{ 
    for (int i = 1; i <= NUM_VERTICES; i++) 
    {
        cout << "degree" << i <<  DEGREES[i] << endl;
        if (i > 100)
            break;
    }
}

int main() 
{
    auto start = high_resolution_clock::now();
    init_degrees();
    read();
    auto stop = high_resolution_clock::now();
    auto duration = duration_cast<seconds>(stop - start);
    cout << "Degree calculation: " << duration.count() << " seconds" << endl;
    //print_degrees();
    return 0;
}